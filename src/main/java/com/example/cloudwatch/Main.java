// Copyright (c) Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.cloudwatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import java.util.HashMap;
import java.util.Map;

/**
 *  Simple example program to generate log events of varying levels in a loop.
 *  Logs INFO, WARN, and ERROR messages, then flushes and exits.
 */
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Set up some thread context properties (optional, but useful for JSON layout)
        ThreadContext.put("userId", "user123");
        ThreadContext.put("sessionId", "abc-123");

        for (int i = 0; i < 5; i++) {
            logger.info("This is a simple info message. Loop iteration: {}", i);

            Map<String, String> data = new HashMap<>();
            data.put("transactionId", "txn-" + i);
            data.put("amount", String.format("%.2f", 100.00 + i));
            data.put("currency", "USD");
            logger.info("Processed transaction: {}", data);

            logger.warn("This is a warning message. Loop iteration: {}", i);

            try {
                throw new RuntimeException("This is a test exception for demo purposes! Loop iteration: " + i);
            } catch (Exception e) {
                logger.error("An error occurred during processing. Loop iteration: {}", i, e);
            }

            try {
                Thread.sleep(500); // Small delay to simulate work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Clear thread context (optional)
        ThreadContext.clearAll();

        // Log a message larger than CloudWatch Logs maximum (256 KB)
        StringBuilder veryLongMessage = new StringBuilder();
        //for (int i = 0; i < 270 * 1024 / 10; i++) { // Create a string larger than 256KB
        //    veryLongMessage.append("0123456789");
        //}
        //logger.error("This is a very long message designed to exceed CloudWatch Logs maximum size. Length: {} bytes. Content: {}", veryLongMessage.length(), veryLongMessage.toString());


        logger.info("Application finished logging messages. Flushing appenders.");
        org.apache.logging.log4j.LogManager.shutdown(); // Force flush of all appenders
    }
}
