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

(defn populate-map [m]
  (let [size (count m)
        map-data (mapcat (fn [row] (map (fn [tile] (case tile
                                                     "-"  config/wall
                                                     "/"  config/door
                                                     "C"  config/closed-chest
                                                     config/blank)) row)) m)]
    (reset! s/game-map {:size size :values (vec map-data)})

    (doseq [[i tile] (map-indexed vector (string/join m))]
      (let [[x y] (i->coordinates size i)
            id (keyword (str (random-uuid)))]
        (case tile
          "@" (swap! s/entities assoc :player (builders/build-player {:x x
                                                                      :y y
                                                                      :items {:1 {:id :1
                                                                                  :variant :POTION
                                                                                  :name "Red Potion"
                                                                                  :quantity 3
                                                                                  :effects [{:effect :STAT-CHANGE :stat :health :amount 10}]}}}))
          "J" (swap! s/entities assoc id (builders/build-jelly {:id id
                                                                :x x
                                                                :y y}))
          "S" (swap! s/entities assoc id (builders/build-statue {:id id
                                                                 :x x
                                                                 :y y}))
          "X" (swap! s/entities assoc id (builders/build-soldier {:id id
                                                                  :x x
                                                                  :y y}))
          nil)))))

(defn start-game []
  (canvas/init-ctx)
  (register-input-listener)
  (populate-map config/map1)
  (render))
