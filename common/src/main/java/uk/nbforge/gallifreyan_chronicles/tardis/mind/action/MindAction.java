package uk.nbforge.gallifreyan_chronicles.tardis.mind.action;

/**
   One small thing a TARDIS does, written in json with a type field.
   Personalities and traits list these under hooks like on_unwilling.
*/
public interface MindAction {
    /** The type this was loaded from. */
    String type();

    void run(MindContext context);
}
