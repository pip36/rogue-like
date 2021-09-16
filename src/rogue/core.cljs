(ns rogue.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [game.core :as g]
   [game.config :as config]
   [game.state :as state]))

(defn stats []
  [:div#stats
   [:p "Player: " @state/player]
   (for [monster (state/all-monsters)]
     [:p "Monster: " monster])])

(defn game
  []
  (r/create-class
   {:display-name  "game"

    :component-did-mount
    (fn [] (g/start-game))

    :reagent-render
    (fn []
      [:div#container
       [:canvas {:id config/CANVAS-ID
                 :width config/CANVAS-HEIGHT
                 :height config/CANVAS-WIDTH
                 :style {:border "1px solid black"}}]
       [stats]])}))

(defn mount-root []
  (d/render [game] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
