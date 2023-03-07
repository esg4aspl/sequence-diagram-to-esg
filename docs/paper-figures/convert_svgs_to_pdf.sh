find . -name '*.svg' | sed 's/.svg$//g' | xargs -I{} rsvg-convert -f pdf -o {}.pdf {}.svg

