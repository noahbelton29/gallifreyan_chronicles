package uk.nbforge.gallifreyan_chronicles.compat.portal;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;

/**
 * Keeps Voxy inside Immersive Portals' stencil clipping.
 *
 * <p>Immersive Portals draws a portal view with the stencil test on. The opening is stamped with the
 * portal's layer number, and everything drawn in that view is only kept where the stencil matches.
 * Voxy breaks this in two places:
 * <ul>
 *     <li>It switches the stencil test off at the end of {@code renderOpaque} and never switches it back on.
 *     Everything drawn after it in that portal view (entities, block entities such as another TARDIS exterior)
 *     is then no longer clipped to the opening and is painted over whatever surrounds the portal.</li>
 *     <li>Its final composite goes into a scratch framebuffer that only has the depth and colour textures
 *     attached (no stencil), and it turns the stencil test off first. The far terrain is then written over
 *     the whole screen instead of just the opening.</li>
 * </ul>
 *
 * <p>To fix this, the stencil state is remembered when Voxy starts and put back when it is done. For the
 * composite, the stencil buffer is attached to Voxy's scratch framebuffer and the portal's stencil test
 * is kept on.
 *
 * <p>This only happens while a portal view is being drawn, a normal view is untouched. If the stencil
 * cannot be used safely, Voxy is skipped for that portal view instead.
 *
 * <p>Start the game with {@code -Dgc.voxy.skipInPortals=true} to skip Voxy in every portal view. This is
 * useful for checking whether a problem comes from Voxy at all.
 */
public final class VoxyPortalGuard {
    /** Debug switch that skips Voxy in every portal view. */
    private static final boolean SKIP_IN_PORTALS = Boolean.getBoolean("gc.voxy.skipInPortals");

    // Everything below is only touched from the render thread.

    /** True once {@link #begin()} has saved the stencil state, so {@link #end()} knows to put it back. */
    private static boolean active;

    /** True when Voxy's composite has to be clipped to the portal's stencil. */
    private static boolean clip;

    // The portal's stencil state as it was when Voxy started drawing.
    private static boolean stencilWasOn;
    private static int func;
    private static int ref;
    private static int valueMask;
    private static int writeMask;
    private static int opFail;
    private static int opDepthFail;
    private static int opDepthPass;

    /** What kind of object (texture or renderbuffer) holds the stencil buffer being drawn into. */
    private static int stencilType;

    /** GL name of the stencil buffer being drawn into. */
    private static int stencilName;

    /** The scratch framebuffer the stencil is currently attached to, or 0 when none is. */
    private static int attachedTo;

    private VoxyPortalGuard() {
    }

    /**
     * Called when Voxy starts drawing. Saves the portal's stencil state if a portal view is being drawn.
     *
     * @return false when Voxy should not draw in this view at all
     */
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
            // The portal clips with the stencil, so Voxy's composite has to as well. Find the stencil
            // buffer of the framebuffer being drawn into so it can be attached for the composite.
            int framebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
            if (framebuffer == 0) return false;
            stencilType = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_DRAW_FRAMEBUFFER,
                    GL30C.GL_STENCIL_ATTACHMENT, GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            stencilName = GL30C.glGetFramebufferAttachmentParameteri(GL30C.GL_DRAW_FRAMEBUFFER,
                    GL30C.GL_STENCIL_ATTACHMENT, GL30C.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            boolean usable = (stencilType == GL11C.GL_TEXTURE || stencilType == GL30C.GL_RENDERBUFFER) && stencilName != 0;
            if (!usable) return false;
            clip = true;
        }
        active = true;
        return true;
    }

    /** Called when Voxy is done drawing. Puts the portal's stencil state back. */
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

    /** Whether Voxy's composite should be clipped to the portal instead of having the stencil test turned off. */
    public static boolean clippingComposite() {
        return active && clip;
    }

    /**
     * Called right after Voxy binds its scratch framebuffer for the composite.
     *
     * <p>Attaches the portal's stencil buffer to it and turns the portal's stencil test back on. Falls back
     * to Voxy's own unclipped composite if the framebuffer is not complete with the stencil attached.
     */
    public static void prepareClippedBlit() {
        if (!active || !clip) return;

        int framebuffer = GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING);
        if (framebuffer == 0) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
            return;
        }

        attachStencil(framebuffer, stencilName);
        if (GL45C.glCheckNamedFramebufferStatus(framebuffer, GL30C.GL_DRAW_FRAMEBUFFER) != GL30C.GL_FRAMEBUFFER_COMPLETE) {
            // Can't clip with this framebuffer, fall back to Voxy's own unclipped composite.
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

    /** Called when Voxy's composite is done. Takes the stencil off Voxy's scratch framebuffer again. */
    public static void finishClippedBlit() {
        if (attachedTo == 0) return;
        attachStencil(attachedTo, 0);
        attachedTo = 0;
    }

    /** Attaches the given stencil buffer to the framebuffer, or detaches it when {@code name} is 0. */
    private static void attachStencil(int framebuffer, int name) {
        if (stencilType == GL30C.GL_RENDERBUFFER) {
            GL45C.glNamedFramebufferRenderbuffer(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, GL30C.GL_RENDERBUFFER, name);
        } else {
            GL45C.glNamedFramebufferTexture(framebuffer, GL30C.GL_STENCIL_ATTACHMENT, name, 0);
        }
    }
}
