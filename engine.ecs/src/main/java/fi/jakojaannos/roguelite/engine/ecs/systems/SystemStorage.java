package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

class SystemStorage {
    private final List<SystemContext> systems;
    private final List<InternalSystemGroup> systemGroups;
    private final Map<Class<? extends ECSSystem>, SystemContext> contextLookup;
    private final Map<SystemGroup, InternalSystemGroup> groupLookup;

    SystemStorage(
            List<SystemContext> systems,
            List<InternalSystemGroup> systemGroups
    ) {
        this.systems = systems;
        this.systemGroups = systemGroups;
        this.contextLookup = new HashMap<>();
        this.groupLookup = new HashMap<>();
        this.systems.forEach(ctx -> this.contextLookup.put(ctx.getInstance().getClass(),
                                                           ctx));
        this.systemGroups.forEach(group -> this.groupLookup.put(group.getGroup(), group));
    }

    void forEachPrioritized(@NonNull final Consumer<SystemContext> forEach) {
        val dispatchContext = new DispatchContext(this.systems, this.systemGroups);
        Optional<Class<? extends ECSSystem>> nextEntryPoint;
        while ((nextEntryPoint = dispatchContext.findAnyNotDispatched()).isPresent()) {
            Deque<SystemContext> queue = new ArrayDeque<>();
            queue.add(this.contextLookup.get(nextEntryPoint.get()));
            while (!queue.isEmpty()) {
                val systemContext = queue.getFirst();
                if (dispatchContext.isReadyToDispatch(systemContext)) {
                    removeFromQueueAndDispatch(forEach, dispatchContext, queue, systemContext);
                } else {
                    // Does not remove from queue as the system we are queuing dependencies for was
                    // not dispatched. As we always push to the top of the queue, we are guaranteed
                    // to process all dependencies before the system is encountered again.
                    queueDependenciesWithoutRemovingFromQueue(dispatchContext, queue, systemContext);
                }
            }
        }
    }

    private void removeFromQueueAndDispatch(
            @NonNull final Consumer<SystemContext> forEach,
            @NonNull final DispatchContext dispatchContext,
            @NonNull final Deque<SystemContext> queue,
            @NonNull final SystemContext systemContext
    ) {
        queue.removeFirst();
        forEach.accept(systemContext);
        dispatchContext.setDispatched(systemContext);
    }

    private void queueDependenciesWithoutRemovingFromQueue(
            @NonNull final DispatchContext dispatchContext,
            @NonNull final Deque<SystemContext> queue,
            @NonNull final SystemContext systemContext
    ) {
        // TODO: This extensive dependency tree walking here should NOT be necessary. Simplify
        //  things for easier handling of transitive dependencies caused by groups
        //  -   One possible solution is to bake the groups into the dependency graph at build() by
        //      adding the group dependencies to the systems.

        systemContext.getDependencies()
                     .stream()
                     .filter(dispatchContext::notDispatched)
                     .map(this.contextLookup::get)
                     .forEach(queue::addFirst);

        systemContext.getDependencies()
                     .groupDependenciesAsStream()
                     .filter(dispatchContext::notDispatched)
                     .map(groupLookup::get)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .map(this.contextLookup::get)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.groupLookup::get)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.contextLookup::get)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.groupLookup::get)
                     .flatMap(InternalSystemGroup::getDependencies)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.contextLookup::get)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.groupLookup::get)
                     .flatMap(InternalSystemGroup::getGroupDependencies)
                     .filter(dispatchContext::notDispatched)
                     .map(this.groupLookup::get)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.contextLookup::get)
                     .forEach(queue::addFirst);
    }

    Stream<ECSSystem> nonPrioritizedStream() {
        return this.systems.stream().map(SystemContext::getInstance);
    }
}
