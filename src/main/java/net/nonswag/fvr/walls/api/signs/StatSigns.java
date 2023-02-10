package net.nonswag.fvr.walls.api.signs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatSigns {
    private List<StatSign> signs = new ArrayList<>();
}
