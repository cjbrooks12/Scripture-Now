package com.caseybrooks.scripturememory.nowcards.practice;

/**
 * Practice is used to track which verse is currently being worked on, or practiced
 * on. Now this may not always be actively practicing with this verse, but may
 * be editing it, sharing it, etc. Basically, these classes create a more simplified
 * way to deal with a single verse in the database and ensure that it is always
 * up to date with the database whenever you need it. Anytime you need to deal
 * with a single verse, you should funnel it through here first for consistency
 * (with the exception of specific cases like Main and VOTD, because they
 * contain themselves in a similar way to this class, but need to do it in a
 * specific way to ensure consistency with its own logic. This class is much more
 * general, and outside of the single instance in which it is used, should be
 * stateless, unlike Main and VOTD).
 */
public class Practice {
}
