# clchess

Chess application written in Clojurescript. Uses NW.js.

## Requirements

    https://leiningen.org/

    https://nwjs.io/

## Setup

To compile the CSS using garden:

    lein garden once

To get an interactive development environment run:

    lein figwheel

And in another shell:

    cd resources/public/
    nw .

To clean all compiled files:

    lein clean

## License

See LICENSE file
