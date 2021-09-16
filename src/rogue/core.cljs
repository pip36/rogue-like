(ns rogue.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [game.core :as g]
   [game.config :as config]))

(defn game
  []
  (r/create-class
   {:display-name  "game"

    :component-did-mount
    (fn [] (g/start-game))

    :reagent-render
    (fn []
      [:canvas {:id config/CANVAS-ID
                :width config/CANVAS-HEIGHT
                :height config/CANVAS-WIDTH
                :style {:border "1px solid black"}}])}))

(defn mount-root []
  (d/render [game] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
