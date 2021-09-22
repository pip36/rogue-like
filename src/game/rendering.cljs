(ns game.rendering
  (:require [game.config :as config]
            [game.canvas :as canvas]
            [game.state :refer [game-map
                                get-player-coordinates
                                all-entities
                                items]]
            [game.util :refer [clamp
                               i->coordinates
                               add-coordinates]]))

(defn coordinates->pixels [[x y]]
  [(* x config/TILE-SIZE) (* y config/TILE-SIZE)])

(defmulti render-tile (fn [tile _] (:type tile)))

(defmethod render-tile :default [tile coordinates]
  (let [[x y] (coordinates->pixels coordinates)]
    (canvas/draw-rect x y config/TILE-SIZE config/TILE-SIZE (:color tile))))



(defn calculate-scroll-offset-from-player [x y]
  [(clamp (Math/floor (- x (/ config/tiles-per-screen-x 2)))
          0
          (Math/max 0 (- (:size @game-map) config/tiles-per-screen-x)))
   (clamp (Math/floor (- y (/ config/tiles-per-screen-y 2)))
          0
          (Math/max 0 (- (:size @game-map) config/tiles-per-screen-y)))])

(defn render-map []
  (doseq [[i tile] (map-indexed vector (:values @game-map))]
    (let [coordinates (i->coordinates (:size @game-map) i)
          [player-x player-y] (get-player-coordinates)
          [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
      (render-tile
       tile
       (add-coordinates coordinates [(- x-offset) (- y-offset)])))))

(defn render-entity [entity]
  (let [[player-x player-y] (get-player-coordinates)
        [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
    (canvas/draw-rect
     (* config/TILE-SIZE (- (:x entity) x-offset))
     (* config/TILE-SIZE (- (:y entity) y-offset))
     config/TILE-SIZE config/TILE-SIZE
     (:color entity))))

(defn render-entities []
  (doseq [entity (all-entities)]
    (render-entity entity)))

(defn render-items []
  (let [[player-x player-y] (get-player-coordinates)
        [x-offset y-offset] (calculate-scroll-offset-from-player player-x player-y)]
    (doseq [[[x y] item] @items]
      (when (not-empty item) (canvas/draw-rect
                              (* config/TILE-SIZE (- x x-offset))
                              (* config/TILE-SIZE (- y y-offset))
                              config/TILE-SIZE config/TILE-SIZE
                              "cornflowerblue")))))