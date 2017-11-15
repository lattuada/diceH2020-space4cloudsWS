/*
Copyright 2017 Marco Lattuada

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package it.polimi.diceH2020.SPACE4CloudWS.solvers;

import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Matrix;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.MatrixHugeHoleException;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.Solution;

import java.util.Optional;

import lombok.NonNull;

public abstract class MINLPSolver extends AbstractSolver {
   
   public abstract Optional<Double> evaluate(@NonNull Matrix matrix, @NonNull Solution solution) throws MatrixHugeHoleException;

   public abstract void initializeSpj(Solution solution, Matrix matrix);
}
