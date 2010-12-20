/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.resource.diameter.gx.events.avp;

import net.java.slee.resource.diameter.base.events.avp.DiameterURI;
import net.java.slee.resource.diameter.gx.events.avp.ChargingInformation;
import org.mobicents.slee.resource.diameter.base.events.avp.GroupedAvpImpl;

/**
 * @author <a href="mailto:karthikeyan_s@spanservices.com"> Karthikeyan Shanmugam (EmblaCom)</a>
 */
public class ChargingInformationImpl extends GroupedAvpImpl implements ChargingInformation {

  public ChargingInformationImpl() {
    super();
  }

 
  public ChargingInformationImpl(int code, long vendorId, int mnd, int prt, byte[] value) {
    super(code, vendorId, mnd, prt, value);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#getPrimaryChargingCollectionFunctionName()
   */
  public DiameterURI getPrimaryChargingCollectionFunctionName() {
    return getAvpAsDiameterURI(DiameterGxAvpCodes.PRIMARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#getPrimaryEventChargingFunctionName()
   */
  public DiameterURI getPrimaryEventChargingFunctionName() {
    return getAvpAsDiameterURI(DiameterGxAvpCodes.PRIMARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#getSecondaryChargingCollectionFunctionName()
   */
  public DiameterURI getSecondaryChargingCollectionFunctionName() {
    return getAvpAsDiameterURI(DiameterGxAvpCodes.SECONDARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#getSecondaryEventChargingFunctionName()
   */
  public DiameterURI getSecondaryEventChargingFunctionName() {
    return getAvpAsDiameterURI(DiameterGxAvpCodes.SECONDARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#hasPrimaryChargingCollectionFunctionName()
   */
  public boolean hasPrimaryChargingCollectionFunctionName() {
    return hasAvp(DiameterGxAvpCodes.PRIMARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#hasPrimaryEventChargingFunctionName()
   */
  public boolean hasPrimaryEventChargingFunctionName() {
    return hasAvp(DiameterGxAvpCodes.PRIMARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#hasSecondaryChargingCollectionFunctionName()
   */
  public boolean hasSecondaryChargingCollectionFunctionName() {
    return hasAvp(DiameterGxAvpCodes.SECONDARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#hasSecondaryEventChargingFunctionName()
   */
  public boolean hasSecondaryEventChargingFunctionName() {
    return hasAvp(DiameterGxAvpCodes.SECONDARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#setPrimaryChargingCollectionFunctionName(net.java.slee.resource.diameter.base.events.avp.DiameterURI)
   */
  public void setPrimaryChargingCollectionFunctionName(DiameterURI primaryChargingCollectionFunctionName) {
    addAvp(DiameterGxAvpCodes.PRIMARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID, primaryChargingCollectionFunctionName.toString());
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#setPrimaryEventChargingFunctionName(net.java.slee.resource.diameter.base.events.avp.DiameterURI)
   */
  public void setPrimaryEventChargingFunctionName(DiameterURI primaryEventChargingFunctionName) {
    addAvp(DiameterGxAvpCodes.PRIMARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID, primaryEventChargingFunctionName.toString());
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#setSecondaryChargingCollectionFunctionName(net.java.slee.resource.diameter.base.events.avp.DiameterURI)
   */
  public void setSecondaryChargingCollectionFunctionName(DiameterURI secondaryChargingCollectionFunctionName) {
    addAvp(DiameterGxAvpCodes.SECONDARY_CHARGING_COLLECTION_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID, secondaryChargingCollectionFunctionName.toString());
  }

  /*
   * (non-Javadoc)
   *
   * @see net.java.slee.resource.diameter.gx.events.avp.ChargingInformation#setSecondaryEventChargingFunctionName(net.java.slee.resource.diameter.base.events.avp.DiameterURI)
   */
  public void setSecondaryEventChargingFunctionName(DiameterURI secondaryEventChargingFunctionName) {
    addAvp(DiameterGxAvpCodes.SECONDARY_EVENT_CHARGING_FUNCTION_NAME, DiameterGxAvpCodes.TGPP_VENDOR_ID, secondaryEventChargingFunctionName.toString());
  }

}

