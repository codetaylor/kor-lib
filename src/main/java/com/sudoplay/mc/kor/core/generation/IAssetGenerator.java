package com.sudoplay.mc.kor.core.generation;

import java.lang.annotation.Annotation;

/**
 * Created by codetaylor on 11/7/2016.
 */
public interface IAssetGenerator<A extends Annotation> {
  void generate(A annotation);
}
