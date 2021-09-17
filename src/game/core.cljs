(ns game.core
  (:require
   [game.canvas :as canvas]
   [game.state :as s]
   [game.config :as config]
   [game.builders :as builders]
   [game.entity-behaviours :as behaviour]
   [game.util :refer [add-coordinates]]))

(defn render []
  (when (not (= :GAME_OVER @s/game-state))
    (canvas/clear)
    (s/render-map)
    (s/render-entities)))

(defn handle-user-update [key]
  (case key
    :UP (s/try-move (add-coordinates (s/get-player-coordinates) [0 -1]) :UP)
    :DOWN (s/try-move (add-coordinates (s/get-player-coordinates) [0 1]) :DOWN)
    :LEFT (s/try-move (add-coordinates (s/get-player-coordinates) [-1 0]) :LEFT)
    :RIGHT (s/try-move (add-coordinates (s/get-player-coordinates) [1 0]) :RIGHT)
    :O (s/try-open)
    :default nil))

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

(defn populate-map []
  (reset! s/entities {:player (builders/build-player {:x 2
                                                      :y 3})
                      :1 (builders/build-jelly {:id :1 :x 13 :y 13})
                      :2 (builders/build-jelly {:id :2 :x 1 :y 1})
                      :3 (builders/build-jelly {:id :3 :x 6 :y 6})
                      :4 (builders/build-statue {:id :4 :x 9 :y 10})}))

(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (populate-map)
  (render))
