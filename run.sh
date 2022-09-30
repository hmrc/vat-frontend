#!/usr/bin/env bash
sbt -Dlogger.resource=logback-test.xml -Dapplication.router="testOnlyDoNotUseInAppConf.Routes" "run 9732"
