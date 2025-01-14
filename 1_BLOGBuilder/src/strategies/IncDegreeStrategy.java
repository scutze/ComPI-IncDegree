package strategies;

import blogbuilder.ConfigSingle;
import blogbuilder.Main;
import blogbuilder.SpecContainer;
import blogbuilder.World;
import com.rits.cloning.Cloner;
import elements.RandVar;
import factories.ElementFactory;
import factories.augmentation.IncByWorldFactory;
import factories.augmentation.IncDegreeFactory;
import factories.base.RandomSampleFactory;

/**
 * The Increment degree strategy  is intended to test the performance of the lifted Hugin message passing architecture
 * The strategy creates ever larger worlds by adding clusters of randvars that are connected to one single rv.
 * The ljt algorithm should construct a jtree with a high degree from these worlds.
 */
public class IncDegreeStrategy extends WorldCreationStrategy {

	boolean smallWorlds=true;
	
	int[] domainSizes = {10, 100, 1000};

	boolean allQueries = true;

	// In some scenarios, e.g. for evaluation of message passing, no queries are required.
	// If noQueries is set to false, no queries are included at all.
	boolean noQueries = true;

	// Base World Config:
	int logVarCount = smallWorlds ? 1 : 2;
	int randVarCount = 3;
	int factorCount = smallWorlds ? 1 : 2;
	int maxFacArgs = smallWorlds ? 2 : 3;
	int maxRandVarOccurrences = 4;
	int maxRandVarArgs = 2;

	int incrementCount = 10;

	@Override
	public void start() {

		Cloner cloner = new Cloner();

		int globalFileCounter = 0;

		int rerollCount = 1;
		for (int r=0; r < rerollCount; r++) {
			// Create & fill base World
			ConfigSingle.getInstance().getProgressLogger().logWorldStart(globalFileCounter);
			SpecContainer sc = new SpecContainer(domainSizes, logVarCount, randVarCount, factorCount, maxFacArgs, maxRandVarOccurrences, maxRandVarArgs);
			World bw = new World(r,0, allQueries, sc, noQueries);
			ElementFactory baseFactory = new RandomSampleFactory();
			bw.fillWorld(baseFactory, baseFactory, baseFactory);

			World temp_w = null;
			for (int i = 0; i < incrementCount; i++) {
				if (i == 0) {
					temp_w = cloner.deepClone(bw);
				} else {
					ConfigSingle.getInstance().getProgressLogger().logWorldStart(globalFileCounter);
					ElementFactory augFac = new IncDegreeFactory(temp_w, bw);
					SpecContainer sc_loc = new SpecContainer(domainSizes,
							temp_w.getSpecContainer().getLogVarCount() + logVarCount,
							temp_w.getSpecContainer().getRandVarCount() + randVarCount,
							temp_w.getSpecContainer().getFactorCount() + factorCount +1, // incWorld's factors + 1 connecting factor
							maxFacArgs,
							maxRandVarOccurrences,
							maxRandVarArgs);
					temp_w = new World(r, 0, allQueries, sc_loc, noQueries);

					temp_w.fillWorld(augFac);
				}

				temp_w.createFiles();

				Main.sfw.addLine(temp_w.constructFilePath(), temp_w.getLogVars().size(),
						temp_w.getRandVars().size(), temp_w.getFactors().size(), maxRandVarArgs,
						temp_w.searchRealMaxRVOccurence(), maxFacArgs, temp_w.checkAllRVMentioned());
				Main.odl.addLineForWorld(temp_w.constructFilePath(), temp_w);

				ConfigSingle.getInstance().getProgressLogger().logWorldDone(globalFileCounter, i, temp_w);
				globalFileCounter++;
			}
		}
	}
}
