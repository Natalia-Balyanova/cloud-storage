package ru.balyanova.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Command implements Serializable {

    CommandType type;

    public CommandType getType() {
        return type;
    }
}
