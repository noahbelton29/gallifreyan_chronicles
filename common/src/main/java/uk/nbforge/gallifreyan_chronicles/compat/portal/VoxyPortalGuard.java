package uk.nbforge.gallifreyan_chronicles.compat.portal;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

/**
 * Keeps Voxy inside the stencil clipping of Immersive Portals.
 * A portal view is drawn with the stencil test on and only kept where the stencil matches.
 * Voxy breaks this in two places.
 * It switches the stencil test off at the end of renderOpaque and never back on, so everything drawn after it, like entities and other TARDIS exteriors, is painted over what surrounds the portal.
 * Its final composite goes into a scratch framebuffer with no stencil and the test off, so the far terrain covers the whole screen and not just the opening.
 * The fix remembers the stencil state when Voxy starts and puts it back when done.
 * For the composite it attaches the stencil buffer to the scratch framebuffer and keeps the portal test on.
 * This only happens while a portal view is drawn, if the stencil cannot be used safely Voxy is skipped for that view.
 * The debug switch gc.voxy.skipInPortals set to true skips Voxy in every portal view, it helps check if a problem comes from Voxy at all.
 */
public final class VoxyPortalGuard {
    private static final boolean SKIP_IN_PORTALS = Boolean.getBoolean("gc.voxy.skipInPortals");

    // Everything below is only touched from the render thread

    // True once begin has saved the stencil state so end knows to put it back
    private static boolean active;

    // True when the composite of Voxy has to be clipped to the stencil of the portal
    private static boolean clip;

    // The stencil state of the portal from when Voxy started drawing
    private static boolean stencilWasOn;
    private static int func;
    private static int ref;
    private static int valueMask;
    private static int writeMask;
    private static int opFail;
    private static int opDepthFail;
    private static int opDepthPass;

    // The kind of object, texture or renderbuffer, that holds the stencil buffer
    private static int stencilType;

    // The GL name of the stencil buffer being drawn into
    private static int stencilName;

    // The scratch framebuffer the stencil is attached to, 0 when none
    private static int attachedTo;

    private VoxyPortalGuard() {}

    /** Returns false when Voxy should not draw in this view at all. */
    public static boolean begin() {
        active = false;
        clip = false;
        attachedTo = 0;

        if (!TardisPortalBridge.isRenderingAnyPortal()) return true;
        if (SKIP_IN_PORTALS) return false;

        stencilWasOn = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);
        func = GL11C.glGetInteger(GL11C.GL_STENCIL_FUNC);
        ref = GL11C.glGetInteger(GL11C.GL_STENCIL_REF);
        valueMask = GL11C.glGetInteger(GL11C.GL_STENCIL_VALUE_MASK);
        writeMask = GL11C.glGetInteger(GL11C.GL_STENCIL_WRITEMASK);
        opFail = GL11C.glGetInteger(GL11C.GL_STENCIL_FAIL);
        opDepthFail = GL11C.glGetInteger(GL11C.GL_STENCIL_PASS_DEPTH_FAIL);
        opDepthPass = GL11C.glGetInteger(GL11C.GL_STENCIL_PASS_DEPTH_PASS);

        if (stencilWasOn) {
            // The portal clips with the stencil so the composite of Voxy has to as well
            // Find the stencil buffer of the framebuffer being drawn into so it can be attached for the composite
            int framebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            if (framebuffer == 0) return false;
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
            if (!usable) return false;
            clip = true;
        }
        active = true;
        return true;
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
     * Attaches the stencil buffer of the portal and turns its stencil test back on.
     * If the framebuffer is not complete with the stencil attached it falls back to the unclipped composite of Voxy.
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
            /** Cannot clip with this framebuffer so fall back to the unclipped composite of Voxy. */
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

    /** Takes the stencil off the scratch framebuffer again. */
    public static void finishClippedBlit() {
        if (attachedTo == 0) return;
        attachStencil(attachedTo, 0);
        attachedTo = 0;
    }

    /** A name of 0 detaches it. */
    private static void attachStencil(int framebuffer, int name) {
        if (stencilType == GL30C.GL_RENDERBUFFER) {
            GL45C.glNamedFramebufferRenderbuffer(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, GL30C.GL_RENDERBUFFER, name);
        } else {
            GL45C.glNamedFramebufferTexture(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, name, 0);
        }
    }
}
