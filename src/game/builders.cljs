(ns game.builders
  (:require [game.config :as config]))

(defn build-statue [statue-data]
  (merge config/statue statue-data))

(defn build-jelly [jelly-data]
  (merge config/jelly jelly-data))

(defn build-soldier [soldier-data]
  (merge config/soldier soldier-data))

(defn build-player [player-data]
  (merge config/player player-data))

(defn build-potion [potion-data]
  (merge config/potion potion-data))

(defn build-stat-change-effect [effect-data]
  (merge config/stat-change-effect effect-data))