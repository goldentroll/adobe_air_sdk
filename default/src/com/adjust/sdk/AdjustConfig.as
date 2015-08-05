package com.adjust.sdk {
    public class AdjustConfig {
        private var appToken:String;
        private var environment:String;
        private var logLevel:String;

        private var eventBufferingEnabled:Boolean;

        public function AdjustConfig(appToken:String, environment:String) {
            this.appToken = appToken;
            this.environment = environment;
        }

        public function setLogLevel(logLevel:String):void {
            this.logLevel = logLevel;
        }

        public function setEventBufferingEnabled(eventBufferingEnabled:Boolean):void {
            this.eventBufferingEnabled = eventBufferingEnabled;
        }

        // Getters.
        public function getAppToken():String {
            return this.appToken;
        }

        public function getEnvironment():String {
            return this.environment;
        }

        public function getLogLevel():String {
            return this.logLevel;
        }

        public function getEventBufferingEnabled():Boolean {
            return this.eventBufferingEnabled;
        }
    }
}
