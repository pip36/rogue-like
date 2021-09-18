(ns rogue.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [game.core :as g]
   [game.config :as config]
   [game.state :as state]))

(defn debug []
  [:div#debug
   [:p "------------------------------------------------"]
   [:p "Game status: " @state/game-state]
   [:p "Player: " (:player @state/entities)]
   [:p "Inventory " (str @state/inventory)
    (for [monster (state/all-monsters)]
      [:p {:key (:id monster)} "Monster: " monster])]])

(defn stats []
  [:div#stats
   [:p [:strong "Health: "] (-> @state/entities :player :health)]
   [:p [:strong "Gold: "] (-> @state/inventory :gold)]])

(defn events []
  [:div#events
   (for [event @state/events]
     [:p event])])

(defn game
  []
  (r/create-class
   {:display-name  "game"

    :component-did-mount
    (fn [] (g/start-game))

    :reagent-render
    (fn []
      [:div#container
       [:div#game-container
        [:div {:style {:display "flex"}}
         [:canvas {:id config/CANVAS-ID
                   :height config/CANVAS-HEIGHT
                   :width config/CANVAS-WIDTH
                   :style {:border "1px solid black"}}]
         [events]]
        [stats]]
       [debug]])}))

(defn mount-root []
  (d/render [game] (.getElementById js/document "app")))

(defn ^:export init! []
  (mount-root))
