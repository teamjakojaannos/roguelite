module roguelite.game {
    requires static lombok;
    requires org.slf4j;
    requires org.lwjgl.opengl;
    requires org.joml;

    requires roguelite.engine;
    requires roguelite.engine.ecs;

    exports fi.jakojäännös.roguelite.game;
    exports fi.jakojäännös.roguelite.game.view;
    exports fi.jakojäännös.roguelite.game.data;
}