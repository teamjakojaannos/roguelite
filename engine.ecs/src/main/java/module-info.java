module roguelite.engine.ecs {
    requires static lombok;
    requires org.slf4j;
    requires jsr305;

    requires roguelite.engine.utilities;

    exports fi.jakojaannos.roguelite.engine.ecs;
}
