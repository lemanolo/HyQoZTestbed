package org.lig.hadas.aesop.tests;

import org.lig.hadas.aesop.experiments.HypatiaToHyQoZMapper;
import org.lig.hadas.aesop.qwGeneration.model.QueryWorkflow;
import org.lig.hadas.hybridqp.QEPBuilder;
import org.lig.hadas.hybridqp.QEPNode;
import org.lig.hadas.hybridqp.Exceptions.CyclicHypergraphException;
import org.lig.hadas.hybridqp.Log.Log;

public class ConnectingToHypatia {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String serviceInstancesFileName   = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_providers.txt";
		String serviceInterfacesFileName  = "/Users/aguacatin/Research/HADAS/workspace_hypatia/Hypatia/BM_TONATIUH_interfaces.txt";

		Log.off();
		QEPBuilder builder = new QEPBuilder(serviceInstancesFileName, serviceInterfacesFileName);
		Log.on();
		String query; 
//		query = "SELECT op1_op1::op1_op1_i_a, op1_op1::op1_op1_o_a, op1_op1::op1_op1_o_b, op1_op1::op1_op1_o_c, op1_op1::op1_op1_o_d, op1_op1::op1_op1_o_e, op1_op1::op1_op1_o_f, " +
//				"       op9_op9::op9_op9_i_a, op9_op9::op9_op9_o_a, op9_op9::op9_op9_o_b, op9_op9::op9_op9_o_c, op9_op9::op9_op9_o_d, op9_op9::op9_op9_o_e, op9_op9::op9_op9_o_f "+
//				"  FROM op1_op1 as op1_op1,"+
//				"       op9_op9 as op9_op9"+
//				" WHERE op1_op1::op1_op1_i_a='val_r_i_a'"+
//				"   AND op9_op9::op9_op9_i_a='val_r_i_a'"+
//				"   AND op9_op9::op9_op9_o_d=op1_op1::op1_op1_o_e;";
//		query= "SELECT op1_op1.op1_op1_i_a, op1_op1.op1_op1_o_a, op1_op1.op1_op1_o_b, " +
//			   "       op1_op1.op1_op1_o_c, op1_op1.op1_op1_o_d, op1_op1.op1_op1_o_e, " +
//			   "       op1_op1.op1_op1_o_f, op3_op3.op3_op3_i_a, op3_op3.op3_op3_o_a, " +
//			   "       op3_op3.op3_op3_o_b, op3_op3.op3_op3_o_c, op3_op3.op3_op3_o_d, " +
//			   "       op3_op3.op3_op3_o_e, op3_op3.op3_op3_o_f " +
//			   "  FROM op1_op1 as op1_op1, op3_op3 as op3_op3 " +
//			   " WHERE op1_op1.op1_op1_i_a='val_r_op1_op1_i_a' " +
//			   "   AND op3_op3.op3_op3_i_a=op1_op1.op1_op1_o_f;";
//		query = "SELECT op10_op10.op10_op10_i_a, op10_op10.op10_op10_o_a, op10_op10.op10_op10_o_b, op10_op10.op10_op10_o_c, op10_op10.op10_op10_o_d, op10_op10.op10_op10_o_e, op10_op10.op10_op10_o_f, " +
//				"       op1_op1.op1_op1_i_a, op1_op1.op1_op1_o_a, op1_op1.op1_op1_o_b, op1_op1.op1_op1_o_c, op1_op1.op1_op1_o_d, op1_op1.op1_op1_o_e, op1_op1.op1_op1_o_f, op3_op3.op3_op3_i_a, op3_op3.op3_op3_o_a, op3_op3.op3_op3_o_b, op3_op3.op3_op3_o_c, op3_op3.op3_op3_o_d, op3_op3.op3_op3_o_e, op3_op3.op3_op3_o_f " +
//				"  FROM op10_op10 as op10_op10, op1_op1 as op1_op1, op3_op3 as op3_op3 " +
//				" WHERE op10_op10.op10_op10_i_a=op1_op1.op1_op1_o_f " +
//				"   AND op1_op1.op1_op1_i_a='val_r_op1_op1_i_a' " +
//				"   AND op3_op3.op3_op3_i_a=op1_op1.op1_op1_o_f;";
		query = "SELECT op1_op1.op1_op1_i_a, op1_op1.op1_op1_o_a, op1_op1.op1_op1_o_b, op1_op1.op1_op1_o_c, op1_op1.op1_op1_o_d, op1_op1.op1_op1_o_e, op1_op1.op1_op1_o_f, " +
				"       op9_op9.op9_op9_i_a, op9_op9.op9_op9_o_a, op9_op9.op9_op9_o_b, op9_op9.op9_op9_o_c, op9_op9.op9_op9_o_d, op9_op9.op9_op9_o_e, op9_op9.op9_op9_o_f" +
				"  FROM op1_op1 as op1_op1, op9_op9 as op9_op9" +
				" WHERE op1_op1.op1_op1_i_a = 'val_r_op1_op1_i_a' " +
				"   AND op9_op9.op9_op9_i_a = 'val_r_op9_op9_i_a' " +
				"   AND op9_op9.op9_op9_o_d = op1_op1.op1_op1_o_e;";
		query = "SELECT op10_op10.op10_op10_i_a, op10_op10.op10_op10_o_a, op10_op10.op10_op10_o_b, op10_op10.op10_op10_o_c, op10_op10.op10_op10_o_d, op10_op10.op10_op10_o_e, op10_op10.op10_op10_o_f, " +
				"\n       op11_op11.op11_op11_i_a, op11_op11.op11_op11_o_a, op11_op11.op11_op11_o_b, op11_op11.op11_op11_o_c, op11_op11.op11_op11_o_d, op11_op11.op11_op11_o_e, op11_op11.op11_op11_o_f, " +
				"\n       op1_op1.op1_op1_i_a, op1_op1.op1_op1_o_a, op1_op1.op1_op1_o_b, op1_op1.op1_op1_o_c, op1_op1.op1_op1_o_d, op1_op1.op1_op1_o_e, op1_op1.op1_op1_o_f, " +
				"\n       op3_op3.op3_op3_i_a, op3_op3.op3_op3_o_a, op3_op3.op3_op3_o_b, op3_op3.op3_op3_o_c, op3_op3.op3_op3_o_d, op3_op3.op3_op3_o_e, op3_op3.op3_op3_o_f, " +
				"\n       op5_op5.op5_op5_i_a, op5_op5.op5_op5_o_a, op5_op5.op5_op5_o_b, op5_op5.op5_op5_o_c, op5_op5.op5_op5_o_d, op5_op5.op5_op5_o_e, op5_op5.op5_op5_o_f, " +
				"\n       op6_op6.op6_op6_i_a, op6_op6.op6_op6_o_a, op6_op6.op6_op6_o_b, op6_op6.op6_op6_o_c, op6_op6.op6_op6_o_d, op6_op6.op6_op6_o_e, op6_op6.op6_op6_o_f, " +
				"\n       op7_op7.op7_op7_i_a, op7_op7.op7_op7_o_a, op7_op7.op7_op7_o_b, op7_op7.op7_op7_o_c, op7_op7.op7_op7_o_d, op7_op7.op7_op7_o_e, op7_op7.op7_op7_o_f, " +
				"\n       op9_op9.op9_op9_i_a, op9_op9.op9_op9_o_a, op9_op9.op9_op9_o_b, op9_op9.op9_op9_o_c, op9_op9.op9_op9_o_d, op9_op9.op9_op9_o_e, op9_op9.op9_op9_o_f" +
				"\n  FROM op10_op10 as op10_op10, " +
				"\n       op11_op11 as op11_op11, " +
				"\n       op1_op1 as op1_op1, " +
				"\n       op3_op3 as op3_op3, " +
				"\n       op5_op5 as op5_op5, " +
				"\n       op6_op6 as op6_op6, " +
				"\n       op7_op7 as op7_op7, " +
				"\n       op9_op9 as op9_op9" +
				"\n WHERE op10_op10.op10_op10_i_a = op1_op1.op1_op1_o_f " +
				"\n   AND op11_op11.op11_op11_i_a = op9_op9.op9_op9_o_f " +
				"\n   AND op1_op1.op1_op1_i_a = 'val_r_op1_op1_i_a' " +
				"\n   AND op3_op3.op3_op3_i_a = 'val_r_op3_op3_i_a' " +
				"\n   AND op3_op3.op3_op3_o_f = op7_op7.op7_op7_o_c " +
				"\n   AND op5_op5.op5_op5_i_a = 'val_r_op5_op5_i_a' " +
				"\n   AND op6_op6.op6_op6_i_a = 'val_r_op6_op6_i_a' " +
				"\n   AND op6_op6.op6_op6_o_c = op10_op10.op10_op10_o_a " +
				"\n   AND op7_op7.op7_op7_i_a = op5_op5.op5_op5_o_f " +
				"\n   AND op7_op7.op7_op7_o_d = op1_op1.op1_op1_o_b " +
				"\n   AND op9_op9.op9_op9_i_a = op7_op7.op7_op7_o_f;";


		query=query.replaceAll("::", ".");
		System.out.println(query);
		QEPNode root;
		try {
			Log.off();
			root = builder.constructQEP(query);
			Log.on();
			System.out.println("-----------------------------------------");
			QueryWorkflow qw = HypatiaToHyQoZMapper.getQW(root);
//			QueryWorkflow qw = builder.getQW(root);
			System.out.println(qw.toFunctor());
		} catch (CyclicHypergraphException e) {
			System.err.println(query+"\n"+e.getMessage());
		}

	}

}
