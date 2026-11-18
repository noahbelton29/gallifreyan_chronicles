package uk.nbforge.gallifreyan_chronicles.compat.portal;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

/**
 * keeps voxy inside immersive portals stencil clipping. portal views draw with the stencil test on and only keep what
 * matches, voxy fucks this up in two places. it turns the stencil test off at the end of renderOpaque and never back
 * on, so everything after it (entities, other tardis exteriors) paints over whats around the portal. and its final
 * composite goes thru a scratch framebuffer with no stencil and the test off, so the far terrain covers the whole
 * screen not just the opening. fix is remember the stencil state when voxy starts and put it back after, and for the
 * composite attach the stencil buffer to the scratch framebuffer and keep the portal test on. only happens while a
 * portal view is drawing, if the stencil cant be used safely voxy just gets skipped for that view.
 * gc.voxy.skipInPortals=true skips voxy in every portal view, handy for checking if a problem is even voxy's fault
 */
public final class VoxyPortalGuard {
    private static final boolean SKIP_IN_PORTALS = Boolean.getBoolean("gc.voxy.skipInPortals");

    private static boolean active;

    private static boolean clip;

    private static boolean stencilWasOn;
    private static int func;
    private static int ref;
    private static int valueMask;
    private static int writeMask;
    private static int opFail;
    private static int opDepthFail;
    private static int opDepthPass;

    private static int stencilType;

    private static int stencilName;

    private static int attachedTo;

    private VoxyPortalGuard() {}

    /** true when voxy shouldnt draw in this view at all */
    public static boolean beginOrSkip() {
        active = false;
        clip = false;
        attachedTo = 0;

        if (!TardisPortalBridge.isRenderingAnyPortal()) return false;
        if (SKIP_IN_PORTALS) return true;

        stencilWasOn = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        func = GL11C.glGetInteger(GL11C.GL_STENCIL_FUNC);
        ref = GL11C.glGetInteger(GL11C.GL_STENCIL_REF);
        valueMask = GL11C.glGetInteger(GL11C.GL_STENCIL_VALUE_MASK);
        writeMask = GL11C.glGetInteger(GL11C.GL_STENCIL_WRITEMASK);
        opFail = GL11C.glGetInteger(GL11C.GL_STENCIL_FAIL);
        opDepthFail = GL11C.glGetInteger(GL11C.GL_STENCIL_PASS_DEPTH_FAIL);
        opDepthPass = GL11C.glGetInteger(GL11C.GL_STENCIL_PASS_DEPTH_PASS);

        if (stencilWasOn) {
            int framebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            if (framebuffer == 0) return true;
            stencilType = GL30C.glGetFramebufferAttachmentParameteri(
                    GL30C.GL_DRAW_FRAMEBUFFER,
                    GL30C.GL_STENCIL_ATTACHMENT,
                    GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            stencilName = GL30C.glGetFramebufferAttachmentParameteri(
                    GL30C.GL_DRAW_FRAMEBUFFER,
                    GL30C.GL_STENCIL_ATTACHMENT,
                    GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            boolean usable =
                    (stencilType == GL11C.GL_TEXTURE || stencilType == GL30C.GL_RENDERBUFFER) && stencilName != 0;
            if (!usable) return true;
            clip = true;
        }
        active = true;
        return false;
    }

    public static void end() {
        if (!active) return;
        active = false;
        clip = false;
        attachedTo = 0;

        if (stencilWasOn) {
            GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        } else {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
        GL11C.glStencilFunc(func, ref, valueMask);
        GL11C.glStencilOp(opFail, opDepthFail, opDepthPass);
        GL11C.glStencilMask(writeMask);
    }

    public static boolean clippingComposite() {
        return active && clip;
    }

    /**
     * attaches the portal's stencil buffer and turns its stencil test back on. if the framebuffer isnt complete with
     * the stencil attached it falls back to voxy's unclipped composite
     */
    public static void prepareClippedBlit() {
        if (!active || !clip) return;

        int framebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        if (framebuffer == 0) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            return;
        }

        attachStencil(framebuffer, stencilName);
        if (GL45C.glCheckNamedFramebufferStatus(framebuffer, GL30C.GL_DRAW_FRAMEBUFFER)
                != GL30C.GL_FRAMEBUFFER_COMPLETE) {
            attachStencil(framebuffer, 0);
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            return;
        }
        attachedTo = framebuffer;

        GL11C.glEnable(GL11C.GL_STENCIL_TEST);
        GL11C.glStencilFunc(func, ref, valueMask);
        GL11C.glStencilOp(GL11C.GL_KEEP, GL11C.GL_KEEP, GL11C.GL_KEEP);
        GL11C.glStencilMask(0);
    }

    public static void finishClippedBlit() {
        if (attachedTo == 0) return;
        attachStencil(attachedTo, 0);
        attachedTo = 0;
    }

    /** a name of 0 detaches it */
    private static void attachStencil(int framebuffer, int name) {
        if (stencilType == GL30C.GL_RENDERBUFFER) {
            GL45C.glNamedFramebufferRenderbuffer(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, GL30C.GL_RENDERBUFFER, name);
        } else {
            GL45C.glNamedFramebufferTexture(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, name, 0);
        }
    }
}
