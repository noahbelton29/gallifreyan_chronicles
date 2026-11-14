package uk.nbforge.gallifreyan_chronicles.tardis.mind.action;

/* one small thing a tardis does, written in json as {"type": "...", ...}.
   personalities and traits list these under hooks like "on_unwilling" */
public interface MindAction {
    /* the "type" this was loaded from */
    String type();

    void run(MindContext context);
}
