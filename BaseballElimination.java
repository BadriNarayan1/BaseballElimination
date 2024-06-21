/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.StdOut;

import java.util.TreeMap;

public class BaseballElimination {
    private final int[] w;
    private final int[] l;
    private final int[] r;
    private final int[][] g;
    private final int numOfTeam;
    private final RedBlackBST<String, Integer> teams;
    private final String[] teamsByIndex;
    private final TreeMap<String, Queue<String>> isEliminated;

    public BaseballElimination(String filename) {
        In reader = new In(filename);
        numOfTeam = Integer.parseInt(reader.readLine());
        // System.out.println(numOfTeam);
        w = new int[numOfTeam];
        l = new int[numOfTeam];
        r = new int[numOfTeam];
        g = new int[numOfTeam][numOfTeam];
        teams = new RedBlackBST<String, Integer>();
        teamsByIndex = new String[numOfTeam];
        isEliminated = new TreeMap<String, Queue<String>>();
        int count = 0;
        while (reader.hasNextLine()) {
            // System.out.println(reader.readLine());
            String helper[] = reader.readLine().trim().split("\\s+");
            // for (String s : helper) {
            //     System.out.println(s);
            // }
            teams.put(helper[0], count);
            teamsByIndex[count] = helper[0];
            isEliminated.put(helper[0], null);
            // System.out.println(isEliminated.containsKey(helper[0]));
            w[count] = Integer.parseInt(helper[1]);
            l[count] = Integer.parseInt(helper[2]);
            r[count] = Integer.parseInt(helper[3]);
            for (int i = 4; i < helper.length; i++) {
                g[count][i - 4] = Integer.parseInt(helper[i]);
            }
            count++;
        }
        // System.out.println(isEliminated.containsKey(teamsByIndex[3]));
    }

    public int numberOfTeams() {
        return w.length;
    }

    public Iterable<String> teams() {
        return teams.keys();
    }

    public int wins(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
        return w[teams.get(team)];
    }

    public int losses(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
        return l[teams.get(team)];
    }

    public int remaining(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
        return r[teams.get(team)];
    }

    public int against(String team1, String team2) {
        if (!teams.contains(team1) || !teams.contains(team2)) {
            throw new IllegalArgumentException();
        }
        return g[teams.get(team1)][teams.get(team2)];
    }

    public boolean isEliminated(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
        if (!isEliminated.containsKey(team)) {
            // System.out.println("1 for false");
            return false;
        }
        if (isEliminated.get(team) != null) {
            return true;
        }

        int numOfGames = (numOfTeam * (numOfTeam - 1)) / 2;
        int numOfVertices = numOfGames + numOfTeam + 2;
        int source = numOfVertices - 2;
        int sink = numOfVertices - 1;
        FlowNetwork network = createNetwork(team);
        if (network == null) {
            return true;
        }
        FordFulkerson answer = new FordFulkerson(network, source, sink);
        // System.out.println("After answer: ");
        for (FlowEdge e : network.adj(source)) {
            // System.out.println(e.capacity() + " " + e.flow());
            if (e.flow() != e.capacity()) {
                Queue<String> bag = new Queue<String>();
                for (int i = 0; i < numOfTeam; i++) {
                    if (answer.inCut(i + numOfGames)) {
                        bag.enqueue(teamsByIndex[i]);
                    }
                }
                isEliminated.put(team, bag);
                return true;
            }
        }
        isEliminated.remove(team);
        // System.out.println("2 for false");
        return false;
    }

    private FlowNetwork createNetwork(String team) {
        int numOfGames = (numOfTeam * (numOfTeam - 1)) / 2;
        int numOfVertices = numOfGames + numOfTeam + 2;
        int source = numOfVertices - 2;
        int sink = numOfVertices - 1;
        int teamIndex = teams.get(team);
        int maxPossibleWin = w[teamIndex] + r[teamIndex];
        FlowNetwork network = new FlowNetwork(numOfVertices);
        int gameVertex = 0;
        // System.out.println("Team index: " + teamIndex);
        for (int i = 0; i < numOfTeam; i++) {
            if (i == teamIndex) {
                continue;
            }
            for (int j = i + 1; j < numOfTeam; j++) {
                if (j == teamIndex) {
                    continue;
                }
                network.addEdge(new FlowEdge(source, gameVertex, g[i][j]));
                // System.out.println("game vertex: " + gameVertex);
                network.addEdge(new FlowEdge(gameVertex, i + numOfGames,
                                             Double.POSITIVE_INFINITY));
                network.addEdge(new FlowEdge(gameVertex, j + numOfGames,
                                             Double.POSITIVE_INFINITY));
                gameVertex++;
            }
            if (maxPossibleWin - w[i] < 0) {
                // System.out.println("Montreal: " + teamsByIndex[i]);
                Queue<String> bag = new Queue<String>();
                bag.enqueue(teamsByIndex[i]);
                isEliminated.put(team, bag);
                return null;
            }
            network.addEdge(new FlowEdge(i + numOfGames, sink, maxPossibleWin - w[i]));
        }
        // System.out.println("Just after: ");
        // for (FlowEdge e : network.adj(source)) {
        //     System.out.println(e.capacity() + " " + e.flow());
        //     System.out.println(e.other(source));
        // }

        return network;
    }

    public Iterable<String> certificateOfElimination(String team) {
        if (!teams.contains(team)) {
            throw new IllegalArgumentException();
        }
        if (!isEliminated.containsKey(team)) {
            return null;
        }
        if (isEliminated.get(team) != null) {
            return isEliminated.get(team);
        }

        int numOfGames = (numOfTeam * (numOfTeam - 1)) / 2;
        int numOfVertices = numOfGames + numOfTeam + 2;
        int source = numOfVertices - 2;
        int sink = numOfVertices - 1;
        FlowNetwork network = createNetwork(team);
        if (network == null) {
            return isEliminated.get(team);
        }
        FordFulkerson answer = new FordFulkerson(network, source, sink);
        for (FlowEdge e : network.adj(source)) {
            if (e.flow() != e.capacity()) {
                Queue<String> bag = new Queue<String>();
                for (int i = 0; i < numOfTeam; i++) {
                    if (answer.inCut(i + numOfGames)) {
                        bag.enqueue(teamsByIndex[i]);
                    }
                }
                isEliminated.put(team, bag);
                return bag;
            }
        }
        isEliminated.remove(team);
        return null;

    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        System.out.println(division.isEliminated("Philadelphia"));
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
