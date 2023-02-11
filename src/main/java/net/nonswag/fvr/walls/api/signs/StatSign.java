package net.nonswag.fvr.walls.api.signs;

import lombok.Getter;
import lombok.Setter;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Position;

@Getter
@Setter
public class StatSign extends Sign {
    private Walls.Sort stat;

    public StatSign(Position position, Walls.Sort stat) {
        super(position);
        this.stat = stat;
    }
}
