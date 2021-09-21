(ns game.core
  (:require
   [game.canvas :as canvas]
   [game.state :as s]
   [game.config :as config]
   [game.builders :as builders]
   [game.entity-behaviours :as behaviour]
   [game.util :refer [add-coordinates i->coordinates]]
   [clojure.string :as string]))

(defn render []
  (when (not (= :GAME_OVER @s/game-state))
    (canvas/clear)
    (s/render-map)
    (s/render-items)
    (s/render-entities)))

(defn handle-user-update [key]
  (case key
    :UP (s/try-move (add-coordinates (s/get-player-coordinates) [0 -1]) :UP)
    :DOWN (s/try-move (add-coordinates (s/get-player-coordinates) [0 1]) :DOWN)
    :LEFT (s/try-move (add-coordinates (s/get-player-coordinates) [-1 0]) :LEFT)
    :RIGHT (s/try-move (add-coordinates (s/get-player-coordinates) [1 0]) :RIGHT)
    :O (s/try-open)
    :P (s/try-pickup)
    :default nil)
  (s/trigger-hunger :player))

(defn update-monsters
  "Loop through all monsters and trigger their movement function."
  []
  (doseq [monster (s/all-monsters)]
    (cond
      (s/player-adjacent? (:x monster) (:y monster)) (s/perform-attack (:id monster) :player)
      :else (let [[x y] (behaviour/movement monster)]
              (s/move-entity (:id monster) x y)))))

(defn register-input-listener []
  (set! (. js/document -onkeydown)
        (fn [e]
          (let [k (config/input-keys (. e -keyCode))]
            (when (not (= :GAME_OVER @s/game-state))
              (handle-user-update k)
              (render)
              (update-monsters)
              (when (not (contains? @s/entities :player)) (s/end-game))
              (render))))))



(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (s/populate-map config/map1 {:items {:1 {:id :1
                                           :variant :POTION
                                           :name "Red Potion"
                                           :quantity 3
                                           :effects [{:effect :STAT-CHANGE :stat :health :amount 10}]}}})
  (render))
