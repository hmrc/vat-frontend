#!/bin/bash

echo "Applying migration DeregisterRequirements"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /deregisterRequirements                       controllers.DeregisterRequirementsController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages (English)"
echo "" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "##  DeregisterRequirements" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "deregisterRequirements.title = deregisterRequirements" >> ../conf/messages.en
echo "deregisterRequirements.heading = deregisterRequirements" >> ../conf/messages.en

echo "Adding messages to conf.messages (Welsh)"
echo "" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "##  DeregisterRequirements" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "deregisterRequirements.title = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "deregisterRequirements.heading = WELSH NEEDED HERE" >> ../conf/messages.cy

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DeregisterRequirements completed"
