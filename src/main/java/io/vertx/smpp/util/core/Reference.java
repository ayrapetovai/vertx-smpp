package io.vertx.smpp.util.core;

//   Copyright 2022 Artem Ayrapetov
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

// TODO arena of objects?

/**
 * This class was made to replace AtomicReference to get rid of memory fence operations.
 * Garbage collector handbook
 * @param <T>
 */
public class Reference<T> {
  private T value;

  public T get() {
    return value;
  }

  public void set(T value) {
    this.value = value;
  }
}
