(ns rogue.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [game.core :as g]
   [game.config :as config]
   [game.state :as state]))

(defn stats []
  [:div#stats
   [:div#events
    [:p "Events"]
    (for [event @state/events]
      [:p event])]
   [:p "------------------------------------------------"]
   [:p "Game status: " @state/game-state]
   [:p "Player: " (:player @state/entities)]
   [:p "Inventory " (str @state/inventory)
    (for [monster (state/all-monsters)]
      [:p {:key (:id monster)} "Monster: " monster])]])

(defn game
  []
  (r/create-class
   {:display-name  "game"

    :component-did-mount
    (fn [] (g/start-game))

    :reagent-render
    (fn []
      [:div#container
       [:div
        [:canvas {:id config/CANVAS-ID
                  :height config/CANVAS-HEIGHT
                  :width config/CANVAS-WIDTH
                  :style {:border "1px solid black"}}]]
       [stats]])}))

(defn mount-root []
  (d/render [game] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
