DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# assumes you already built the image using docker build
$DIR/sbt/sbt.sh run
