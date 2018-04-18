/**
 * Copyright © 2016 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.samples.spark;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvgHumidityData implements Serializable {

    private double value;
    private int count;

    public AvgHumidityData(double value) {
        this.value = value;
        this.count = 1;
    }

    public double getAvgValue() {
        return value / count;
    }

    public int getCount(){
        return count;
    }
    
    public static AvgHumidityData sum(AvgHumidityData a, AvgHumidityData b) {
        return new AvgHumidityData(a.value + b.value, a.count + b.count);
    }

}
