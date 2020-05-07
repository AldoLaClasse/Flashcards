package flashcards

import java.io.File
import java.lang.Exception

data class FlashCard (val term: String, val definition: String, var nbMistakes : Int = 0){
    fun addNbMistake () = nbMistakes ++
    fun resetMistake () {nbMistakes = 0}
}

class Deck {

    private val DeckMap = mutableMapOf<Int, FlashCard>()
    var size: Int = DeckMap.size

    operator fun get(randomInt: Int): FlashCard? =  DeckMap[randomInt]

    fun createCard(card: FlashCard, silent: Boolean = false): Int {
        if (!isUniqueTerm(card.term)) return -2 // si une carte existe déjà on retourne -2
        DeckMap.put(DeckMap.size+1,card)
        if (!silent) println("The pair (\"${card.term}\":\"${card.definition})\" has been added.\n")
        size = DeckMap.size
        return 0
    }

    fun removeCard(t: String, silent: Boolean=false): Boolean {
        val foundPosition = findTerm((t))
        if (foundPosition!=-1) {
            DeckMap.remove (foundPosition)
            if (!silent) println("The card has been removed.")
            size = DeckMap.size
            return (true)
        } else {
            println("Can\'t remove \"$t\": there is no such card.")
            return (false)
        }
    }

    private fun findTerm(term: String): Int {
        //retourne la position de la carte trouvée sinon -1
        DeckMap.forEach {
            if (it.value.term.toLowerCase() == term.toLowerCase()) return it.key
        }
        return -1
    }

    fun checkAnswer(answer: String, flashCardId: Int): Boolean {
        return answer.toLowerCase() == DeckMap[flashCardId]?.definition?.toLowerCase()
    }

    fun getCorrectAnswer(flashCardId: Int): String? {
        return DeckMap[flashCardId]?.definition
    }

    fun isUniqueTerm(term: String): Boolean {
        DeckMap.forEach {
            if (it.value.term.toLowerCase() == term.toLowerCase()) return false
        }
        return true
    }

    fun isUniqueDefinition(definition: String): Boolean {
        DeckMap.forEach {
            if (it.value.definition.toLowerCase() == definition.toLowerCase()) return false
        }
        return true
    }

    fun findTermByDefinition(definition: String): String {
        DeckMap.forEach {
            if (it.value.definition.toLowerCase() == definition.toLowerCase()) {
                return it.value.term
            }
        }
        return "NOT_FOUND"
    }

    fun resetStats()  {
        DeckMap.forEach {
            it.value.resetMistake()
        }
    }

    fun hardestCard(): MutableMap<Int, FlashCard> {
        var highScore = 1
        var i = 0
        val hardestCards  = mutableMapOf<Int, FlashCard>()

        DeckMap.forEach {
            // si == alors on conserve les cartes existantes et on ajoute la nouvelle
            if (it.value.nbMistakes == highScore){
                hardestCards.put(i, FlashCard(it.value.term, it.value.definition, it.value.nbMistakes))
                i++
            }
            //si > alors on vire tout et on ne conserve que le nouveau high score
            else if (it.value.nbMistakes > highScore){
                hardestCards.clear()
                hardestCards.put(i, FlashCard(it.value.term, it.value.definition, it.value.nbMistakes))
                i++
                highScore = it.value.nbMistakes
            }
        }
        return (hardestCards)
    }

    fun checkDeckAction(inputAction: String): Boolean {
        return when (inputAction) {
            "add", "remove", "import", "export", "ask", "log", "hardest card", "reset stats", "exit" -> (true)
            else -> (false)
        }
    }

    fun export(fileName :String, newFile:Boolean=false) : Int{
        // retourne le nombre de lignes enregistrées dans le fichier

        var n=0
        try {
            // on enregistre une ligne clé;valeur par carte avec le séparateur ;
            if (newFile) File(fileName).writeText("")
            DeckMap.forEach {
                File(fileName).appendText("${it.value.term};${it.value.definition};${it.value.nbMistakes}\n")
                n++
            }

        }
        catch (e : Exception){
            println(e.message)
            n = -1 // en cas d'erreur on ne retourne pas le nombre de cartes, mais -1
        }
        finally {
            return(n)
        }
    }

    fun load(fileName: String) : Int{
        // retourne le nombre de cartes chargées depuis le fichier

        var n = 0
        if (File(fileName).exists()){
            try{
                // parcours de chaqye ligne pour charger la map
                val lines = File(fileName).readLines()

                for (line in lines){
                    val lCard = line.split(';')
                    var retourCreation = createCard(FlashCard(lCard[0], lCard[1], lCard[2].toInt()),true)
                    while (retourCreation == -2){ //la carte existe déjà il faut faire une mise à jour
                        // je suis fainéant je faire un remove + un create hahaha
                        removeCard(line.substringBefore(';'),true)
                        retourCreation=createCard(FlashCard(lCard[0], lCard[1], lCard[2].toInt()),true)
                    }

                    size = DeckMap.size
                    n++
                }
            }
            catch (e : Exception){
                println(e.message)
                return(-1) // si on n'arrive pas à charger le fichier on retourne -1 au lieu du nombre de cartes chargées
            }
            finally {
                // rien de particulier
            }
        }
        else println("File not found.\n")

        return(n)
    }
}

// cette classe LogFile représente un fichier de log à utiliser pendant le déroulement de l'application
class LogFile (fileName: String = ""){

    var isLogActive = false
    private var name : String = fileName
    private var firstWrite = true

    // fonction qui println et qui log ce qu'on demande,et retourne true si OK et false si KO
    fun printAndLog (strToBeLogged : String) : Boolean {

        var isItOK = true
        try {
            if (firstWrite) {
                if (isLogActive) {
                    File(this.name).writeText(strToBeLogged + "\n")
                    firstWrite = false
                }
                println(strToBeLogged)
            }
            else {
                if (isLogActive) {
                    File(this.name).appendText(strToBeLogged + "\n")
                }
                println(strToBeLogged)
            }
        } catch (e: Exception) {
            println(e.message)
            isItOK =false

        } finally {
            return (isItOK)
        }
    }

    // fonction qui read l'entrée standard et qui log ce qui est entré et retourne "KO" ou la valeur de l'entrée utilsiateur
    fun readAndLog () : String {

        var userInput = ""

        try {
            userInput = readLine()!!
            if (isLogActive) {
                File(this.name).appendText(userInput + "\n")
            }
        }
        catch (e: Exception) {
            println(e.message)
            userInput = "KO"

        } finally {
            return (userInput)
        }
    }

}

fun jouerLeJeu (n :Int, leJeu :Deck, lelog : LogFile) : Int {

    var bonnesReponses = 0

    for (i in 1..n){
        // sortir une carte au hasard
        val randomInt = (1..leJeu.size).shuffled().first()
        println("Print the definition of \"${leJeu[randomInt]?.term}\":")
        val answer = lelog.readAndLog()
        if (leJeu.checkAnswer(answer, randomInt)) {
            lelog.printAndLog("Correct answer.")
            bonnesReponses++
        }
        else {
            val strSortie = "Wrong answer. The correct one is \"${leJeu.getCorrectAnswer(randomInt)}\""
            leJeu[randomInt]?.addNbMistake()
            if (leJeu.isUniqueDefinition(answer)) lelog.printAndLog(strSortie + ".\n")
            else {
                lelog.printAndLog("$strSortie, you've just written the definition of \"${leJeu.findTermByDefinition(answer)}\".\n")
            }
        }
    }
    return(bonnesReponses)
}

// cherche la valeur de l'argument demandé
// exemple si on demande la valeur de -import on chope la chaîne qui suit
// pas super robuste mais bon...
fun readArgument (arguments : Array<String>, s : String) : String {

    var returnNext = false

    arguments.forEach {
        if (returnNext) return (it)
        if (it.equals(s)) returnNext = true
    }

    return "KO"
}

fun main(args: Array<String>) {


    val monJeu = Deck()
    var fichierLog = LogFile()
    var argImportFile = "KO"
    var argExportFile = "KO"
    fichierLog.isLogActive = false
    var action: String
    var fname: String

    // récupération / vérification des arguments de la ligne de commande
    if (args.size != 0){
        argImportFile = readArgument(args, "-import")
        argExportFile = readArgument(args, "-export")
    }

    if (!argImportFile.equals("KO")){
        // import : chegement du jeu depuis un fichier
        fname = argImportFile
        val nbcartesAjoutees = monJeu.load(fname)
        if (nbcartesAjoutees>0) fichierLog.printAndLog("$nbcartesAjoutees cards have been loaded.\n")
    }

    println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
    action = readLine()!!.toString()
    var exporttonewfile: Boolean // pour exporter les cartes, ou pas, dans un fichier

    loop@ while (monJeu.checkDeckAction(action.toLowerCase())) {

        var actionConnue = false
        when(action.toLowerCase()) {

            "add" -> {
                // add : ajout d'une carte
                fichierLog.printAndLog("The card:")
                val term = fichierLog.readAndLog()

                if (!monJeu.isUniqueTerm(term)) {
                    fichierLog.printAndLog("The card \"$term\" already exists. Try again:\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\")")
                    action = fichierLog.readAndLog()
                    actionConnue = true
                }
                else {
                    fichierLog.printAndLog("The definition of the card:")
                    val definition = fichierLog.readAndLog()
                    if (!monJeu.isUniqueDefinition(definition)) {
                        fichierLog.printAndLog("The definition \"$definition\" already exists. Try again:\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):\")")
                        action = fichierLog.readAndLog()
                        actionConnue = true
                    }
                    else monJeu.createCard(FlashCard(term, definition))
                }
            }

            "remove" -> {
                // remove : suppression d'une carte
                fichierLog.printAndLog("The card:")
                val term = fichierLog.readAndLog()
                monJeu.removeCard(term)
            }

            "ask" -> {
                // ask : demander combien de cartes on va jouer et lancer le jeu
                fichierLog.printAndLog("How many times to ask?")
                val nbCartes = fichierLog.readAndLog().toInt()
                jouerLeJeu(nbCartes,monJeu, fichierLog)
            }

            "export" -> {
                // export : sauvegarde du jeu dans un fichier
                fichierLog.printAndLog("File name:")
                fname = fichierLog.readAndLog()
                exporttonewfile = true
                if (exporttonewfile) {
                    fichierLog.printAndLog("${monJeu.export(fname, true)} cards have been saved.\n")
                    exporttonewfile = false
                }
                else fichierLog.printAndLog("${monJeu.export(fname)} cards have been saved.\n")
            }

            "import" -> {
                // import : chegement du jeu depuis un fichier
                fichierLog.printAndLog("File name:")
                fname = fichierLog.readAndLog()
                val nbcartesAjoutees = monJeu.load(fname)
                if (nbcartesAjoutees>0) fichierLog.printAndLog("$nbcartesAjoutees cards have been loaded.\n")
            }

            "log" -> {
                // log : on log tout ce qui se passe dans un fichier log
                println("File name:")
                fname = readLine()!!
                fichierLog = LogFile(fname)
                fichierLog.isLogActive = true
                fichierLog.printAndLog("The log has been saved.\n")
            }

            "hardest card" -> {
                val hardCards = monJeu.hardestCard()
                when (hardCards.count()) {
                    0 -> fichierLog.printAndLog("There are no cards with errors.\n")
                    1 -> {
                        hardCards.forEach() {
                            fichierLog.printAndLog("The hardest card is \"${it.value.term}\". You have ${it.value.nbMistakes} errors answering it.\n")
                        }
                    }
                    else -> {
                        var strHardest = ""
                        hardCards.forEach() {
                            strHardest += "\"${it.value.term}\", "
                        }
                        strHardest = strHardest.substringBeforeLast(',')
                        fichierLog.printAndLog("The hardest cards are $strHardest. You have ${hardCards[0]?.nbMistakes} errors answering them.\n")
                    }
                }
            }

            "reset stats" -> {
                monJeu.resetStats()
                fichierLog.printAndLog("Card statistics has been reset.\n")
            }

            "exit" -> {
                println("Bye bye !")
                // si argument export existe on sauvegarde le jeu au moment de sortir du programme
                if (!argExportFile.equals("KO")) {
                    fname = argExportFile
                    fichierLog.printAndLog("${monJeu.export(fname, true)} cards have been saved.\n")
                }
                break@loop
            }

        }
        // demander quelle est la prochaine action
        if (!actionConnue) {
            fichierLog.printAndLog("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            action = fichierLog.readAndLog()
        }
    }

}
