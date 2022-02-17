#!/usr/bin/env bash
sbt -Dapplication.router="testOnlyDoNotUseInAppConf.Routes" "run 9732"
