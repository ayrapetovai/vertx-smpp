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


/**
 * This class was made to replace AtomicInteger to get rid of memory fence operations.
 */
public class IntegerBox {
  private int value;

  public IntegerBox(int value) {
    this.value = value;
  }

  public int get() {
    return value;
  }

  public void set(int value) {
    this.value = value;
  }

  public int getAndIncrement() {
    return value++;
  }
}
