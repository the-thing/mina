package org.apache.mina.transport.socket.nio;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintTestNameRule implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintTestNameRule.class);

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                LOGGER.error("Running test case: {}-{}", description.getClassName(), description.getMethodName());
                base.evaluate();
            }
        };
    }
}