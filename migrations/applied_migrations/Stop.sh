#!/bin/bash

echo "Applying migration Stop"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /stop               controllers.StopController.onPageLoad()" >> ../conf/app.routes
echo "POST       /stop               controllers.StopController.onSubmit()" >> ../conf/app.routes

echo "Adding messages to conf.messages (English)"
echo "" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "##  Stop" >> ../conf/messages.en
echo "#######################################################" >> ../conf/messages.en
echo "stop.title = stop" >> ../conf/messages.en
echo "stop.heading = stop" >> ../conf/messages.en
echo "stop.option1 = stop" Option 1 >> ../conf/messages.en
echo "stop.option2 = stop" Option 2 >> ../conf/messages.en
echo "stop.checkYourAnswersLabel = stop" >> ../conf/messages.en
echo "stop.error.required = Please give an answer for stop" >> ../conf/messages.en

echo "Adding messages to conf.messages (Welsh)"
echo "" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "##  Stop" >> ../conf/messages.cy
echo "#######################################################" >> ../conf/messages.cy
echo "stop.title = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "stop.heading = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "stop.option1 = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "stop.option2 = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "stop.checkYourAnswersLabel = WELSH NEEDED HERE" >> ../conf/messages.cy
echo "stop.error.required = WELSH NEEDED HERE" >> ../conf/messages.cy

echo "Adding navigation default to NextPage Object"
awk '/object/ {\
     print;\
     print "";\
     print "  implicit val stop: NextPage[StopId.type,";\
     print "    Stop] = {";\
     print "    new NextPage[StopId.type, Stop] {";\
     print "      override def get(b: Stop)(implicit urlHelper: UrlHelper): Call =";\
     print "        b match {";\
     print "          case models.Stop.Option1 => routes.IndexController.onPageLoad()";\
     print "          case models.Stop.Option2 => routes.IndexController.onPageLoad()";\
     print "        }";\
     print "     }";\
     print "  }";\
     next }1' ../app/utils/NextPage.scala > tmp && mv tmp ../app/utils/NextPage.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Stop completed"
