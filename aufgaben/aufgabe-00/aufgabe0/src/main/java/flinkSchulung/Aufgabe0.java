/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flinkSchulung;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;

import java.util.concurrent.ThreadLocalRandom;

 class Aufgabe0 {

	private static final long[] automaten = { 12345, 23456, 34567, 45678, 56789};
	private static final int[] betraege = { 10, 20, 50, 100, 200, 500 };
	private static final String[] personen = { "Klaus A.", "Ferdinand B.", "Lisa C.", "Rosa D.", "Maximilian E." };


	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataGeneratorSource<Abhebung> abhebungSource =
				new DataGeneratorSource<Abhebung>(
						index -> newRandomAbhebung(),
						3600,
						RateLimiterStrategy.perSecond(1),
						TypeInformation.of(Abhebung.class)
				);
		DataStream<Abhebung> input = env.fromSource(abhebungSource, WatermarkStrategy.noWatermarks(),
				"mainSource");
		input.print();

		env.execute();
	}

	private static Abhebung newRandomAbhebung() {
		int automatenIndex = ThreadLocalRandom.current().nextInt(0, automaten.length);
		int betragIndex = ThreadLocalRandom.current().nextInt(0, betraege.length);
		int personenIndex = ThreadLocalRandom.current().nextInt(0, personen.length);

		return new Abhebung(automaten[automatenIndex], personen[personenIndex], betraege[betragIndex]);
	}
}
