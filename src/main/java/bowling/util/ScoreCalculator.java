package bowling.util;

import bowling.api.Roll;
import lombok.experimental.UtilityClass;
import org.springframework.util.ObjectUtils;
import static bowling.util.RollType.LAST_ROLL;
import static bowling.util.RollType.STRIKE;
import static bowling.util.RollType.SPARE;

import java.util.List;

@UtilityClass
public class ScoreCalculator {
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
            RollType rollType = rollType(roll);
            score += canCalculateFrame(rollType, firstFutureRoll, secondFutureRoll) ?
                    calculateRollForFrame(rollType, roll, firstFutureRoll, secondFutureRoll) : 0;

            // Set "secondFuture roll" on LAST ROLL so can calculate any strikes in last frame
            secondFutureRoll = rollType.equals(LAST_ROLL) ? roll : firstFutureRoll;
            firstFutureRoll = roll;
        }
        return score;
    }

    // TODO - add tests for this
    private boolean canCalculateFrame(RollType rollType, Roll firstFutureRoll, Roll secondFutureRoll) {
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

    // TODO -- add tests for this
    private int calculateRollForFrame(RollType rollType, Roll roll, Roll firstFutureRoll, Roll secondFutureRoll) {
        switch (rollType) {
            case STRIKE:
                return lastFrame(roll) ? roll.getPins() : 10 + firstFutureRoll.getPins() + secondFutureRoll.getPins();
            case SPARE:
                return lastFrame(roll) ? roll.getPins() : roll.getPins() + firstFutureRoll.getPins();
            default:
                return roll.getPins();
        }
    }

    // TODO -- add tests for this
    private RollType rollType(Roll roll) {
        return  lastRoll(roll) ? RollType.LAST_ROLL :
                roll.isSpare() ? SPARE :
                        roll.isStrike() ? STRIKE :
                                RollType.get(roll.getThrowForFrame());
    }

    private boolean lastFrame(Roll roll) {
        return roll.getFrame() == 10;
    }

    private boolean lastRoll(Roll roll) {
        return RollType.get(roll.getThrowForFrame()).equals(LAST_ROLL);
    }
}
