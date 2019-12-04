package fi.jakojaannos.roguelite.game.data.components;

import lombok.val;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public interface Shape {
    List<Vector2d> getVertices(
            Transform transform,
            List<Vector2d> result
    );

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
