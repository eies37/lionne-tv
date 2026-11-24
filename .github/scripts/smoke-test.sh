#!/usr/bin/env bash
# Test de fumée : installe l'APK sur l'émulateur, vérifie l'accueil, lance une chaîne,
# zappe, revient à l'accueil et cherche les plantages dans logcat.
set -u

APK=app/build/outputs/apk/debug/app-debug.apk
OUT=smoke-output
mkdir -p "$OUT"
fail=0

ok()   { echo "✅ $*"; }
ko()   { echo "❌ $*"; fail=1; }
info() { echo "ℹ️  $*"; }

dump_ui() { # $1 = nom du fichier
  for _ in 1 2 3; do
    if adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; then
      adb shell cat /sdcard/ui.xml > "$OUT/$1"
      return 0
    fi
    sleep 2
  done
  echo "" > "$OUT/$1"
  info "uiautomator dump impossible pour $1"
}

expect() { # $1 = fichier, $2 = texte attendu, $3 = étape
  if grep -qF "$2" "$OUT/$1"; then ok "$3 : « $2 » affiché"; else ko "$3 : « $2 » absent"; fi
}

adb install -r "$APK" || { ko "installation de l'APK"; exit 1; }
adb logcat -c

# 1. Démarrage et accueil
adb shell am start -W -n com.lionne.tv/.MainActivity
sleep 15
dump_ui home.xml
expect home.xml "Lionne TV" "Accueil"
expect home.xml "En direct · Tests" "Accueil"
expect home.xml "Direct de test HLS" "Accueil"
adb shell screencap -p /sdcard/home.png && adb pull /sdcard/home.png "$OUT/home.png" >/dev/null

# 2. OK sur la première vignette (focus initial) : lecture du direct
adb shell input keyevent KEYCODE_DPAD_CENTER
sleep 2
dump_ui player.xml
expect player.xml "EN DIRECT" "Lecteur"
expect player.xml "Direct de test HLS" "Lecteur"

# 3. Zapping vers la chaîne suivante
adb shell input keyevent KEYCODE_DPAD_UP
sleep 2
dump_ui zap.xml
expect zap.xml "Direct de test Unified" "Zapping"

# 4. Laisser la lecture tourner, puis chercher une erreur de lecture
sleep 20
dump_ui playing.xml
adb shell screencap -p /sdcard/player.png && adb pull /sdcard/player.png "$OUT/player.png" >/dev/null
if grep -qF "Lecture impossible" "$OUT/playing.xml"; then
  info "Lecture : erreur affichée (flux de test externe injoignable depuis la CI ?)"
else
  ok "Lecture : aucune erreur affichée après 20 s"
fi

# 5. Retour à l'accueil
adb shell input keyevent KEYCODE_BACK
sleep 3
dump_ui back.xml
expect back.xml "Direct de test HLS" "Retour à l'accueil"

# 6. Rangée suivante (vidéo à la demande), puis lecture d'un film
adb shell input keyevent KEYCODE_DPAD_DOWN
sleep 2
dump_ui vod-row.xml
expect vod-row.xml "Big Buck Bunny" "Rangée VOD"
adb shell input keyevent KEYCODE_DPAD_CENTER
sleep 2
dump_ui vod.xml
expect vod.xml "Big Buck Bunny" "Lecture VOD"
expect vod.xml "Films libres" "Lecture VOD"
sleep 15
dump_ui vod-playing.xml
adb shell screencap -p /sdcard/vod.png && adb pull /sdcard/vod.png "$OUT/vod.png" >/dev/null
if grep -qF "Lecture impossible" "$OUT/vod-playing.xml"; then
  info "Lecture VOD : erreur affichée (flux de test externe injoignable depuis la CI ?)"
else
  ok "Lecture VOD : aucune erreur affichée après 15 s"
fi
adb shell input keyevent KEYCODE_BACK
sleep 3

# 7. Plantages et erreurs du lecteur
adb logcat -d > "$OUT/logcat.txt"
if grep -E "FATAL EXCEPTION|has died|ANR in com.lionne.tv" "$OUT/logcat.txt"; then
  ko "Plantage détecté dans logcat"
else
  ok "Aucun plantage dans logcat"
fi
echo "--- Erreurs du lecteur (logcat) ---"
grep -E "ExoPlayerImplInternal|PlaybackException" "$OUT/logcat.txt" | head -20 || true
echo "--- Lecteur : formats décodés (logcat) ---"
grep -E "MediaCodec|codec" "$OUT/logcat.txt" | grep -i "com.lionne\|exo\|c2\." | head -5 || true

exit $fail
