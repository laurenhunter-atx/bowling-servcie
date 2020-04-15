package bowling.util

import bowling.api.Roll
import spock.lang.Specification

class ScoreCalculatorSpec extends Specification {

    def "calculate score for player"() {
        when:
        Roll roll1 = roll(1, 1, 5)

        then: "[5,-]"
        assert ScoreCalculator.calculate([roll1]) == 0

        when:
        Roll roll2 = roll(1, 2, 4)

        then: "[5,4]"
        assert ScoreCalculator.calculate([roll1, roll2]) == 9

        when:
        Roll roll3 = roll(2, 1, 10, true)

        then: "[5,4][x]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3]) == 9

        when:
        Roll roll4 = roll(3, 1, 4)

        then: "[5,4][x][4,-]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4]) == 9

        when:
        Roll roll5 = roll(3, 2, 0)

        then: "[5,4][x][4,0]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4, roll5]) == 27

        when:
        Roll roll6 = roll(4, 1, 5)

        then: "[5,4][x][4,0][5,-]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4, roll5, roll6]) == 27

        when:
        Roll roll7 = roll(4, 2, 5, false, true)

        then: "[5,4][x][4,0][5,5]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4, roll5, roll6, roll7]) == 27

        when:
        Roll roll8 = roll(5, 1, 10, true)

        then: "[5,4][x][4,0][5,5][x]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8]) == 47

        when:
        Roll roll9 = roll(6, 1, 10, true)

        then: "[5,4][x][4,0][5,5][x][x]"
        assert ScoreCalculator.calculate([roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8, roll9]) == 47

        when:
        Roll roll10 = roll(7, 1, 10, true)

        then: "[5,4][x][4,0][5,5][x][x][x]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5,
                roll6, roll7, roll8, roll9, roll10]) == 77

        when:
        Roll roll11 = roll(8, 1, 10, true)

        then: "[5,4][x][4,0][5,5][x][x][x][x]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5,
                roll6, roll7, roll8, roll9, roll10, roll11]) == 107

        when:
        Roll roll12 = roll(9, 1, 4)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,-]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6,
                roll7, roll8, roll9, roll10, roll11, roll12]) == 131

        when:
        Roll roll13 = roll(9, 2, 6, false, true)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7,
                roll8, roll9, roll10, roll11, roll12, roll13]) == 151



        when: "LAST FRAME COMBO [6,4,3] : spare"
        Roll lastFrameRoll1 = roll(10, 1, 6, false, false)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][6,-]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7,
                roll8, roll9, roll10, roll11, roll12, roll13, lastFrameRoll1]) == 167

        when:
        Roll lastFrameRoll2 = roll(10, 2, 4, false, true)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][6,4,-]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8,
                roll9, roll10, roll11, roll12, roll13, lastFrameRoll1, lastFrameRoll2]) == 167

        when:
        Roll lastFrameRoll3 = roll(10, 3, 3, false, false)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][6,4,3]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8, roll9, roll10,
                roll11, roll12, roll13, lastFrameRoll1, lastFrameRoll2, lastFrameRoll3]) == 180



        when: "ANOTHER LAST FRAME COMBO [x,x,x] : strikes"
        lastFrameRoll1 = roll(10, 1, 10, true, false)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][x,-]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7,
                roll8, roll9, roll10, roll11, roll12, roll13, lastFrameRoll1]) == 171

        when:
        lastFrameRoll2 = roll(10, 2, 10, true, false)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][x,x]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8,
                roll9, roll10, roll11, roll12, roll13, lastFrameRoll1, lastFrameRoll2]) == 171

        when:
        lastFrameRoll3 = roll(10, 3, 10, true, false)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][x,x,x]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8, roll9, roll10,
                roll11, roll12, roll13, lastFrameRoll1, lastFrameRoll2, lastFrameRoll3]) == 201




        when: "ANOTHER LAST FRAME COMBO [1, 2] : no 3rd roll"
        lastFrameRoll1 = roll(10, 1, 1)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][1,-]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7,
                roll8, roll9, roll10, roll11, roll12, roll13, lastFrameRoll1]) == 162

        when:
        lastFrameRoll2 = roll(10, 2, 2)

        then: "[5,4][x][4,0][5,5][x][x][x][x][4,6][1,2]"
        assert ScoreCalculator.calculate([
                roll1, roll2, roll3, roll4, roll5, roll6, roll7, roll8,
                roll9, roll10, roll11, roll12, roll13, lastFrameRoll1, lastFrameRoll2]) == 165
    }


    def roll(int frame, int throwForFrame, int pins, boolean strike = false, boolean spare = false) {
        return Roll.builder()
                .frame(frame)
                .spare(spare)
                .strike(strike)
                .throwForFrame(throwForFrame)
                .pins(pins)
                .build()
    }
}
