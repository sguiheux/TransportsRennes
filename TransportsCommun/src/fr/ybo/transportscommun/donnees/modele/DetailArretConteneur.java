/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ybo.transportscommun.donnees.modele;

public class DetailArretConteneur {

    private int horaire;
    private int trajetId;
    private int sequence;

    public DetailArretConteneur(int horaire, int trajetId, int sequence) {
        super();
        this.horaire = horaire;
        this.trajetId = trajetId;
        this.sequence = sequence;
    }

    public int getHoraire() {
        return horaire;
    }

    public int getTrajetId() {
        return trajetId;
    }

    public int getSequence() {
        return sequence;
    }

}
