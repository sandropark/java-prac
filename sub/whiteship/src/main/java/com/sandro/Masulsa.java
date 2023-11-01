package com.sandro;

import net.bytebuddy.ByteBuddy;

import java.io.File;
import java.io.IOException;

import static net.bytebuddy.implementation.FixedValue.value;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Masulsa {
    public static void main(String[] args) {
        makeRedefinedMoja();
        System.out.println(new Moja().pullOut());
    }

    private static void makeRedefinedMoja() {
        try {
            new ByteBuddy().redefine(Moja.class)
                    .method(named("pullOut"))
                    .intercept(value("Rabbit!"))
                    .make()
                    .saveIn(new File("/Users/sandro/java-prac/sub/whiteship/out/production/classes"))
            ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
