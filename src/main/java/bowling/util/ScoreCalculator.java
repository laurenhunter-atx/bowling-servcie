package bowling.util;

import bowling.api.Roll;
import lombok.experimental.UtilityClass;
import org.springframework.util.ObjectUtils;

import java.util.List;

@UtilityClass
public class ScoreCalculator {
    private final static int FIRST_THROW_FOR_FRAME = 1;
    private final static int SECOND_THROW_FOR_FRAME = 1;
    private final static int LAST_ROLL = 3;
    private final static int STRIKE = 4;
    private final static int SPARE = 5;

    /*
        Starting from the last roll and moving towards the first roll,
        compute frame score if available. Store "future rolls" for in order
        to calculate strikes or spares.
     */
    public static int calculate(List<Roll> rolls) {
        int score = 0;
        if (rolls.isEmpty()) return score;

        Roll firstFutureRoll = null;
        Roll secondFutureRoll = null;
        for(int i = rolls.size() - 1; i >= 0; i--) {
            Roll roll = rolls.get(i);
            int rollType = rollType(roll);
            score += canCalculateFrame(rollType, firstFutureRoll, secondFutureRoll) ?
                    calculateRollForFrame(rollType, roll, firstFutureRoll, secondFutureRoll) : 0;

            // Set "secondFuture roll" on LAST ROLL so can calculate any strikes in last frame
            secondFutureRoll = rollType == LAST_ROLL ? roll : firstFutureRoll;
            firstFutureRoll = roll;
        }
        return score;
    }

    private boolean canCalculateFrame(int rollType, Roll firstFutureRoll, Roll secondFutureRoll) {
        switch (rollType) {
            case STRIKE:
               return !ObjectUtils.isEmpty(firstFutureRoll) && !ObjectUtils.isEmpty(secondFutureRoll);
            case SPARE:
                return !ObjectUtils.isEmpty(firstFutureRoll);
            case FIRST_THROW_FOR_FRAME:
                return !ObjectUtils.isEmpty(firstFutureRoll) && (!firstFutureRoll.isSpare() || !ObjectUtils.isEmpty(secondFutureRoll));
            default:
                return true;
        }
    }

    private int calculateRollForFrame(int rollType, Roll roll, Roll firstFutureRoll, Roll secondFutureRoll) {
        switch (rollType) {
            case STRIKE:
                return lastFrame(roll) ? roll.getPins() : 10 + firstFutureRoll.getPins() + secondFutureRoll.getPins();
            case SPARE:
                return lastFrame(roll) ? roll.getPins() : roll.getPins() + firstFutureRoll.getPins();
            default:
                return roll.getPins();
        }
    }

    private int rollType(Roll roll) {
        return  lastRoll(roll) ? LAST_ROLL :
                roll.isSpare() ? SPARE :
                        roll.isStrike() ? STRIKE :
                                roll.getThrowForFrame();
    }

    private boolean lastFrame(Roll roll) {
        return roll.getFrame() == 10;
    }

    private boolean lastRoll(Roll roll) {
        return roll.getThrowForFrame() == LAST_ROLL;
    }
}
