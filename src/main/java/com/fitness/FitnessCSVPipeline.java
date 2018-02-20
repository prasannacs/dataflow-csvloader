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
package com.fitness;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableRow;

/**
 * A starter example for writing Beam programs.
 *
 * <p>
 * The example takes two strings, converts them to their upper-case
 * representation and logs them.
 *
 * <p>
 * To run this starter example locally using DirectRunner, just execute it
 * without any additional parameters from your favorite development environment.
 *
 * <p>
 * To run this starter example using managed resource in Google Cloud Platform,
 * you should specify the following command-line options: --project=
 * <YOUR_PROJECT_ID> --stagingLocation=
 * <STAGING_LOCATION_IN_CLOUD_STORAGE> --runner=DataflowRunner
 */
public class FitnessCSVPipeline {
	private static final Logger LOG = LoggerFactory.getLogger(FitnessCSVPipeline.class);

	public static void main(String[] args) {

		System.out.println("CSV Loader triggerred ..");
		for (int i = 0; i < args.length; i++)
			System.out.println("args " + args[i]);

		//Pipeline p = Pipeline.create(PipelineOptionsFactory.fromArgs(args).withValidation().create());
		PipelineOptionsFactory.register(MyOptions.class);
		MyOptions options = PipelineOptionsFactory.fromArgs(args).withoutStrictParsing().as(MyOptions.class);
		Pipeline p = Pipeline.create(options);

		String BUCKET_NAME = "gs://fitness-data/" + args[1].substring(17);

		PCollection<String> lines = p.apply(TextIO.read().from(BUCKET_NAME));
		PCollection<TableRow> row = lines.apply(ParDo.of(new StringToRowConverter()));

		row.apply(BigQueryIO.<TableRow> writeTableRows()
				.to("sixth-hawk-194719:fitness_data.member_fitness_tracker_history")
				// .withSchema(getSchema())
				.withWriteDisposition(WriteDisposition.WRITE_APPEND)
				.withCreateDisposition(CreateDisposition.CREATE_NEVER));

		// p.apply(Create.of("Hello", "World"))
		// .apply(MapElements.via(new SimpleFunction<String, String>() {
		// @Override
		// public String apply(String input) {
		// return input.toUpperCase();
		// }
		// }))
		// .apply(ParDo.of(new DoFn<String, Void>() {
		// @ProcessElement
		// public void processElement(ProcessContext c) {
		// LOG.info(c.element());
		// }
		// }));

		p.run();
	}

	// StringToRowConverter
	static class StringToRowConverter extends DoFn<String, TableRow> {
		@ProcessElement
		public void processElement(ProcessContext c) {
			String[] split = c.element().split(",");
			// c.output(new TableRow().set("",c.element()));
			TableRow row = new TableRow();
			row.set("Member_ID", split[0]);
			row.set("First_Name", split[1]);
			row.set("Last_Name", split[2]);
			row.set("Gender", split[3]);
			row.set("Age", split[4]);
			row.set("Height", split[5]);
			row.set("Weight", split[6]);
			row.set("Hours_Sleep", split[7]);
			row.set("Calories_Consumed", split[8]);
			row.set("Exercise_Calories_Burned", split[9]);
			row.set("Date", split[10]);
			c.output(row);
		}
	}
}
