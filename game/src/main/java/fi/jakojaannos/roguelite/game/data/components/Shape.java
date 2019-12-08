package fi.jakojaannos.roguelite.game.data.components;

import lombok.val;
import org.joml.Rectangled;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public interface Shape {
    List<Vector2d> getVertices(
            Transform transform,
            List<Vector2d> result
    );

    default Rectangled getBounds(final Transform transform) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        val vertices = getVertices(transform, new ArrayList<>());
        for (val vertex : vertices) {
            minX = Math.min(minX, vertex.x);
            maxX = Math.max(maxX, vertex.x);
            minY = Math.min(minY, vertex.y);
            maxY = Math.max(maxY, vertex.y);
        }

        return new Rectangled(minX, minY, maxX, maxY);
    }

    default Vector2d supportPoint(
            final Transform transform,
            final Vector2d direction,
            final Vector2d result
    ) {
        val normDirection = direction.normalize(new Vector2d());
        val vertices = getVertices(transform, new ArrayList<>(4));

        var maxProduct = normDirection.dot(vertices.get(0));
        var index = 0;
        for (var i = 1; i < vertices.size(); ++i) {
            val product = normDirection.dot(vertices.get(i));
            if (product > maxProduct) {
                maxProduct = product;
                index = i;
            }
        }

        return result.set(vertices.get(index));
    }
}
