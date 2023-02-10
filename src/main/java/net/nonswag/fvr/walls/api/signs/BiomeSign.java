package net.nonswag.fvr.walls.api.signs;

import lombok.Getter;
import lombok.Setter;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Position;

@Getter
@Setter
public class BiomeSign extends Sign {
    private Walls.Team team;

    public BiomeSign(Position position, Walls.Team team) {
        super(position);
        this.team = team;
    }
}
