(defproject yaqp "0.2.0"
  :description "Yet Another EverQuest Parser"
  :url "https://github.com/kocubinski/yaqp"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [commons-io/commons-io "2.4"]
                 [net.java.dev.jna/jna "4.0.0"]
                 [simple-time "0.2.0"]
                 [seesaw "1.4.5"]
                 [net.sf.sociaal/freetts "1.2.2"]]
  :java-source-paths ["src/java"])
