#Action Additional Info

  Additional Information is an JsonObject on an Action which has optional information about the perform action.
The JsonObject is initialized on the constructor of Action and can be modified with a Set method.
Some optional information are useful for the MoveChecker.

  Example of optional information are:

* {FAMILYMEMBER_ID: 0}
* {INDEX_EFFECT: 0 }
* {CARDNAME : "card_name"} required by some effects to open a more human friendly context
