package com.acme.shared.vo;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

public record Id(UUID uuid) {
    public static Id withId(String id){
        return new Id(UuidCreator.fromString(id));
    }
    public static Id withoutId(){
        return new Id(UuidCreator.getTimeOrderedEpoch());
    }
    public String stringfyId() {
        return this.uuid.toString();
    }
}
