package com.caseybrooks.scripturememory.nowcards.input;

/**
 * Input is more than just entering a verse through the InputCard on the Dashboard,
 * it generalizes all data coming into the app. This may be the user entering
 * a reference that needs to be parsed and combined with the verse's text. It may
 * be text being shared from another app that we need to make sense of. It may
 * be parsing an XML file to load verses into the app. It may also be editing
 * a verse or its many components. All these things that currently exist everywhere
 * throughout the app (and are often written several times because of this), should
 * be handled exclusively here, with all these parts delegating here.
 *
 * Just to be consistent, we will also include the class used to output verses
 * to our own XML format, so that input and output of this format stays close
 * together and easy to work on at the same time.
 */
public class Input {
}
